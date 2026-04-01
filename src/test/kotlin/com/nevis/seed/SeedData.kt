package com.nevis.seed

import com.nevis.model.CreateClientRequest
import com.nevis.model.CreateDocumentRequest

data class ClientSeed(
    val client: CreateClientRequest,
    val documents: List<CreateDocumentRequest>,
)

object SeedData {

    val johnDoe = ClientSeed(
        client = CreateClientRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@neviswealth.com",
            description = "Senior portfolio manager specialising in equities and fixed income strategies.",
            socialLinks = listOf("https://linkedin.com/in/johndoe"),
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Utility Bill - March 2026",
                content = "Electricity bill for John Doe at 45 King Street, London EC2V 8AQ. Account number 7734921. Amount due: £142.50. Due date: 15 April 2026. Billing period: 1 March 2026 to 31 March 2026. Meter reading: 45892. Previous reading: 45210. Units consumed: 682 kWh. Tariff: Economy 7 variable rate. Supplier: British Gas. Payment method: direct debit from Barclays current account. This document serves as proof of address for the account holder and has been verified against the electoral register.",
            ),
            CreateDocumentRequest(
                title = "Investment Policy Statement",
                content = "Investment Policy Statement for John Doe, prepared by Nevis Wealth Management. Client objectives: long-term capital appreciation with moderate risk tolerance over a 15-year investment horizon. Target asset allocation: 60% global equities (split 40% developed markets, 20% emerging markets), 30% fixed income (investment grade corporate bonds and gilts), 10% alternatives (infrastructure and private equity). Rebalancing will occur quarterly when allocations drift more than 5% from targets. Maximum single-stock exposure: 5% of portfolio. ESG preferences: exclude tobacco, controversial weapons, and thermal coal. Benchmark: composite of MSCI World (60%), Bloomberg Global Aggregate (30%), and HFRI Fund of Funds (10%). Annual review scheduled for January each year. Risk metrics: maximum acceptable drawdown of 20%, target Sharpe ratio above 0.8. Liquidity requirement: £50,000 accessible within 5 business days for emergency needs.",
            ),
        ),
    )

    val aliceSmith = ClientSeed(
        client = CreateClientRequest(
            firstName = "Alice",
            lastName = "Smith",
            email = "alice.smith@goldmansachs.com",
            description = "High-net-worth client with diversified investments across real estate and technology.",
            socialLinks = listOf("https://linkedin.com/in/alicesmith"),
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "KYC Verification Report",
                content = "Know Your Customer verification report for Alice Smith. Verification date: 12 January 2026. Identity confirmed via UK passport (number 543219876, expiry 2032). Secondary identification: UK driving licence. Address verified against utility bill dated December 2025. Source of wealth: proceeds from the sale of a technology company (SmithTech Solutions Ltd) acquired by a major US tech firm in 2023 for approximately £15 million. Employment status: non-executive director at two FTSE 250 companies. Politically Exposed Person screening: negative. Sanctions screening: negative. Adverse media screening: no material findings. Risk classification: standard. Enhanced due diligence: not required. Next review due: January 2027. Verification performed by: Compliance Team, Nevis Wealth Management.",
            ),
            CreateDocumentRequest(
                title = "Property Valuation Report",
                content = "Independent valuation of residential property at 12 Chelsea Harbour, London SW10. Valuation date: 5 February 2026. Estimated market value: £3,200,000. Property type: three-bedroom penthouse apartment with Thames river views. Gross internal area: 1,850 sq ft. Leasehold with 987 years remaining. Service charge: £12,500 per annum. The property was purchased in 2019 for £2,750,000 and has appreciated approximately 16% since acquisition. Current rental estimate: £6,500 per calendar month. The property is unencumbered with no outstanding mortgage. It is held as part of the client real estate portfolio alongside two other London properties. Surveyor: Knight Frank Residential. This valuation has been prepared in accordance with RICS Valuation Standards.",
            ),
        ),
    )

    val bobWilliams = ClientSeed(
        client = CreateClientRequest(
            firstName = "Bob",
            lastName = "Williams",
            email = "bob.williams@neviswealth.com",
            description = "Retired executive focused on wealth preservation and estate planning.",
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Estate Planning Summary",
                content = "Comprehensive estate planning summary for the Williams family. A discretionary trust (The Williams Family Trust) was established on 3 March 2020 with an initial settlement of £325,000 (within the nil-rate band). Primary beneficiaries: two adult children (Robert Williams Jr. and Sarah Williams-Hughes) and three grandchildren (ages 8, 12, and 15). The trust holds a diversified investment portfolio currently valued at approximately £480,000. Life insurance: whole-of-life policy with Legal and General, sum assured £500,000, written in trust. Pension arrangements: defined benefit pension from former employer (annual income £42,000) and SIPP valued at £385,000 with drawdown commencing at age 75. Charitable legacy: 10% of residual estate pledged to the Royal British Legion. Power of attorney: lasting powers of attorney for both health and financial decisions registered with the Office of the Public Guardian, with Robert Jr. as primary attorney. Will last updated: September 2025.",
            ),
            CreateDocumentRequest(
                title = "Bank Statement - February 2026",
                content = "Current account statement for Bob Williams. Account: Barclays Premier, sort code 20-45-12. Statement period: 1 February 2026 to 28 February 2026. Opening balance: £45,230.18. Closing balance: £42,870.55. Key transactions: pension income from BAE Systems pension scheme £3,500.00, state pension £915.40, council tax direct debit -£245.00, private healthcare (Bupa) -£385.00, Waitrose groceries -£487.23, BT broadband -£54.99, Thames Water -£68.40, charitable donation to Royal British Legion -£100.00, transfer to savings account -£2,000.00. This statement can serve as proof of address and proof of income for the account holder.",
            ),
            CreateDocumentRequest(
                title = "Comprehensive Annual Financial Review 2025",
                content = "Annual financial review for Robert (Bob) Williams, prepared by Nevis Wealth Management, covering the period 1 January 2025 to 31 December 2025. " +
                    "Executive Summary: This review provides a holistic assessment of Mr Williams' financial position, including investment performance, pension arrangements, estate planning, tax efficiency, and insurance provisions. Total net worth as at 31 December 2025 is estimated at £2,450,000, representing an increase of approximately 6.2% from the prior year. " +
                    "Investment Portfolio Review: The discretionary investment portfolio managed by Nevis Wealth Management returned 7.3% net of fees over the calendar year, outperforming the composite benchmark return of 6.1% by 120 basis points. The portfolio is allocated as follows: UK equities 25% (FTSE 100 large-cap dividend payers), global equities 20% (diversified across US, European, and Asian markets), fixed income 35% (a mix of UK gilts, investment-grade corporate bonds, and index-linked gilts), alternatives 10% (infrastructure funds and absolute return strategies), and cash equivalents 10% (money market funds and short-term deposits). " +
                    "The UK equity allocation benefited from strong performance in the energy and healthcare sectors, while the global equity allocation was supported by continued growth in US technology stocks. The fixed income allocation provided stability during periods of market volatility, with index-linked gilts offering inflation protection. The alternatives allocation delivered consistent returns through infrastructure investments in renewable energy projects. " +
                    "Pension Arrangements: The defined benefit pension from BAE Systems continues to provide a reliable income of £42,000 per annum, fully index-linked to CPI. The SIPP, valued at £385,000 as at 31 December 2025, is invested in a cautious growth strategy and has not yet entered drawdown. It is recommended that drawdown commence at age 75 to optimise tax efficiency and preserve the pension fund for as long as possible. The state pension provides an additional £11,500 per annum. " +
                    "Estate Planning Update: The Williams Family Trust, established in 2020, continues to hold a diversified portfolio now valued at approximately £480,000. No distributions were made from the trust during 2025. The next ten-year periodic charge is not due until March 2030. The whole-of-life insurance policy with Legal and General (sum assured £500,000) remains in force with premiums of £350 per month. It is recommended that the trust deed be reviewed in light of recent changes to inheritance tax legislation announced in the Autumn Statement. " +
                    "Tax Planning: Total income for the 2024-2025 tax year was approximately £58,000, comprising pension income and investment returns. The personal allowance was fully utilised. Capital gains of £4,200 were realised from the sale of unit trusts, well within the annual exempt amount. ISA contributions of £20,000 were made to maximise tax-free investment growth. It is recommended that Mr Williams consider making additional pension contributions via carry-forward of unused annual allowance from previous tax years. " +
                    "Insurance Review: Private healthcare cover through Bupa remains in place at an annual premium of £4,620. Home and contents insurance is provided by Aviva at an annual premium of £1,200. The whole-of-life policy is held in trust as noted above. No additional insurance needs have been identified. " +
                    "Recommendations for 2026: (1) Review trust deed in light of inheritance tax changes, (2) Consider commencing SIPP drawdown planning, (3) Explore carry-forward pension contributions, (4) Rebalance portfolio to increase fixed income allocation given improved gilt yields, (5) Schedule meeting with solicitor to update will. " +
                    "Next review scheduled for January 2027. This report has been prepared by the Nevis Wealth Management advisory team and should be read in conjunction with the detailed portfolio valuation report and trust accounts.",
            ),
        ),
    )

    val mariaGarcia = ClientSeed(
        client = CreateClientRequest(
            firstName = "Maria",
            lastName = "Garcia",
            email = "maria.garcia@jpmorgan.com",
            description = "Institutional client managing pension fund allocations.",
            socialLinks = listOf("https://linkedin.com/in/mariagarcia"),
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Pension Fund Mandate",
                content = "Investment mandate for the Garcia Corporate Pension Scheme, effective from 1 October 2024. Total assets under management: £125,000,000 as at 31 December 2025. Investment strategy: liability-driven investment (LDI) framework with a growth portfolio overlay. LDI allocation (60%): gilt-based strategy hedging 85% of interest rate and inflation sensitivity of scheme liabilities. Growth portfolio (40%): diversified across global equities (15%), multi-asset credit (10%), infrastructure (8%), and private equity (7%). Funding level: 94% on a technical provisions basis, target full funding by 2030. Scheme actuary: Mercer. Investment consultant: Cambridge Associates. Quarterly reporting to the board of trustees required. De-risking trigger: increase LDI allocation by 5% for every 3% improvement in funding level. Next triennial valuation due: March 2027. Responsible investment: signatory to the UN Principles for Responsible Investment.",
            ),
            CreateDocumentRequest(
                title = "Quarterly Performance Report Q4 2025",
                content = "Quarterly investment performance report for the Garcia Corporate Pension Scheme, Q4 2025 (1 October to 31 December 2025). Overall portfolio return: 3.2% gross of fees versus composite benchmark return of 2.8%, representing outperformance of 40 basis points. Attribution analysis: growth portfolio contributed 2.1% (benchmark 1.7%), driven primarily by strong performance in technology equities and global infrastructure funds. LDI portfolio contributed 1.1% (benchmark 1.1%), performing in line with liability movements. Top contributors: global technology equity allocation returned 8.2% as semiconductor and AI-related holdings rallied strongly. Infrastructure funds benefited from inflation-linked revenue streams. Detractors: UK small-cap allocation underperformed by 1.5% due to domestic economic uncertainty. Multi-asset credit was flat as spread tightening offset carry. Recommendation: rebalance growth portfolio towards fixed income given improved gilt yields and the scheme approaching its de-risking trigger at 97% funding. Estimated funding level at quarter end: 96%.",
            ),
        ),
    )

    val jamesChen = ClientSeed(
        client = CreateClientRequest(
            firstName = "James",
            lastName = "Chen",
            email = "james.chen@outlook.com",
            description = "Tech entrepreneur seeking venture capital and growth equity opportunities.",
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Company Articles of Incorporation",
                content = "Articles of incorporation for Chen Technologies Ltd. Company number: 14523876. Registered in England and Wales on 15 June 2021. Registered office: 88 Shoreditch High Street, London E1 6JJ. Share capital: 10,000 ordinary shares of £1 each, fully paid up. Sole director and majority shareholder: James Chen (holding 8,500 shares). Minority shareholders: Angel Investor Group Ltd (1,000 shares) and Dr. Wei Chen (500 shares, family member). Company objects: development and commercialisation of artificial intelligence software for financial services. The company has adopted model articles for private companies limited by shares with the following amendments: pre-emption rights on share transfers requiring board approval, drag-along and tag-along provisions for minority shareholders, and a deadlock resolution mechanism through independent mediation. Annual accounts filed up to 30 June 2025. Corporation tax reference: UTR 1234567890.",
            ),
            CreateDocumentRequest(
                title = "Tax Return Summary 2024-2025",
                content = "Self-assessment tax return summary for James Chen, tax year 2024-2025 (6 April 2024 to 5 April 2025). Total income: £285,000. Breakdown: employment income as CTO of Chen Technologies Ltd: £120,000 (PAYE). Dividend income from Chen Technologies Ltd: £85,000 (at 33.75% higher rate). Capital gains: £60,000 from disposal of shares in a previous startup venture (Quantum Analytics Ltd, acquired 2019, sold January 2025). Rental income from buy-to-let property in Manchester: £20,000 gross (£14,500 net after allowable expenses). Total tax liability: £98,450. Comprised of: income tax £72,300, capital gains tax £12,000 (after annual exempt amount), National Insurance £14,150. Payments on account for 2025-2026: two instalments of £49,225 each due January and July 2026. Tax return submitted electronically on 28 January 2026. HMRC reference: UTR 1234567890.",
            ),
        ),
    )

    val alexandraMorgan = ClientSeed(
        client = CreateClientRequest(
            firstName = "Alexandra",
            lastName = "Morgan",
            email = "alexandra.morgan@neviswealth.com",
            description = "Family office advisor specialising in multi-generational wealth transfer.",
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Morgan Family Trust Deed",
                content = "Deed of trust for The Morgan Family Trust, a discretionary settlement established on 1 September 2022. Settlor: Alexandra Morgan. Trustees: Alexandra Morgan and Neviswealth Trustees Ltd. Beneficiary class: descendants of Alexandra Morgan and their spouses, with an overriding power of appointment exercisable by the trustees. Initial trust fund: £500,000 in cash and a portfolio of listed securities valued at £1,200,000 at the date of settlement. Trust period: 125 years from the date of settlement. The trust deed includes standard provisions for the investment, accumulation, and distribution of income and capital at the trustees discretion. Letter of wishes dated 15 September 2022 expresses a preference for distributions to support education and first-home purchases for grandchildren. The trust is UK-resident for tax purposes and subject to the relevant property regime with 10-year periodic charges.",
            ),
            CreateDocumentRequest(
                title = "ESG Impact Report 2025",
                content = "Annual ESG impact report for the Morgan family portfolio, prepared by Nevis Wealth Management for the year ended 31 December 2025. Portfolio carbon footprint: 42 tonnes CO2e per £1M invested, a reduction of 18% from the prior year. ESG composite score: 82 out of 100 (MSCI methodology), up from 76 in 2024. Key achievements: full divestment from fossil fuel extraction companies completed in Q2 2025, representing a reallocation of £350,000 into renewable energy infrastructure. The portfolio now holds positions in three dedicated impact funds targeting affordable housing, clean water, and sustainable agriculture. Social impact metrics: portfolio companies employ over 45,000 people across 28 countries, with 62% reporting living wage certification. Governance: 89% of portfolio companies have independent board chairs and 41% have achieved gender parity at board level. Engagement activity: participated in 12 shareholder resolutions on climate disclosure and 5 on executive remuneration. Areas for improvement: increase allocation to green bonds and investigate biodiversity-focused investment opportunities for 2026.",
            ),
        ),
    )

    val alexThompson = ClientSeed(
        client = CreateClientRequest(
            firstName = "Alex",
            lastName = "Thompson",
            email = "alex.thompson@ubs.com",
            description = "Private banking client with focus on sustainable and ESG-aligned investments.",
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Sustainable Investment Portfolio Review",
                content = "Annual portfolio review for Alex Thompson, prepared by UBS Wealth Management, covering the period 1 January 2025 to 31 December 2025. Total portfolio value: £2,850,000. Asset allocation: 55% global equities (with ESG tilt), 25% green bonds, 10% sustainable infrastructure, 10% cash and short-term instruments. Annual return: 9.8% net of fees versus benchmark 8.1%. The equities allocation is screened using UBS proprietary sustainability framework, excluding companies with material exposure to thermal coal, controversial weapons, and tobacco. Notable holdings include positions in First Solar, Orsted, and Vestas Wind Systems. The green bond allocation comprises issuances from the European Investment Bank, World Bank, and several corporate issuers with verified use-of-proceeds. Portfolio turnover for the year: 22%. Dividend income received: £68,500. Unrealised capital gains: £195,000. Recommended actions for 2026: consider adding exposure to carbon credit markets and increase allocation to emerging market green bonds as spreads have widened to attractive levels.",
            ),
            CreateDocumentRequest(
                title = "Risk Assessment Questionnaire",
                content = "Risk profiling questionnaire for Alex Thompson, completed 14 March 2026. Overall risk score: 7 out of 10, corresponding to a moderately aggressive risk profile. Investment experience: 15 years. The client has experience with equities, bonds, structured products, and alternative investments. Investment time horizon: 10 to 15 years. Income requirement: none, all investment income to be reinvested. Capacity for loss: the client can tolerate a temporary portfolio decline of up to 25% without needing to liquidate. Attitude to risk: the client prioritises long-term capital growth and is comfortable with short-term volatility. The client has confirmed understanding of the risks associated with equity investment, concentration risk, and currency exposure. Suitability assessment: the proposed ESG-focused growth strategy is consistent with the client stated objectives and assessed risk tolerance. Review frequency: annual. Next review scheduled: March 2027. Assessment conducted by: Senior Wealth Advisor, UBS London.",
            ),
        ),
    )

    val siobhanOConnor = ClientSeed(
        client = CreateClientRequest(
            firstName = "Siobhan",
            lastName = "O'Connor",
            email = "siobhan.oconnor@alexpartners.com",
            description = "Retired CFO with income-focused portfolio and charitable giving strategy.",
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Retirement Income Plan",
                content = "Retirement income plan for Siobhan O Connor, prepared by Nevis Wealth Management, effective from 1 April 2025. Target annual income: £95,000 after tax, increasing annually with CPI. Income sources: defined benefit pension from former employer (£38,000 per annum, index-linked), state pension (£11,500 per annum from age 67), SIPP drawdown (£32,000 per annum from a pot of £680,000), rental income from a buy-to-let flat in Edinburgh (£13,500 per annum net). The SIPP drawdown rate of 4.7% is sustainable over a 25-year planning horizon assuming average annual investment returns of 5% net of fees and inflation of 2.5%. The SIPP is invested in a cautious growth strategy: 40% investment-grade bonds, 30% global dividend equities, 15% infrastructure, 15% multi-asset income fund. Contingency reserve: £30,000 held in an easy-access savings account for unexpected expenses. Long-term care planning: a whole-of-life insurance policy with a sum assured of £150,000 is in place to help fund potential care costs.",
            ),
            CreateDocumentRequest(
                title = "Charitable Giving Strategy",
                content = "Charitable giving strategy for Siobhan O Connor, reviewed March 2026. Annual charitable budget: £15,000. Giving structure: a donor-advised fund (DAF) held with the Charities Aid Foundation with a current balance of £42,000. Regular donations: £500 per month to Medecins Sans Frontieres, £250 per month to the Royal National Lifeboat Institution, £200 per month to a local hospice in Cork, Ireland. Additional discretionary grants of up to £5,000 per year from the DAF for ad-hoc causes. Tax efficiency: all regular donations made under Gift Aid, providing an effective tax benefit of approximately £3,750 per year. The DAF allows the client to make a lump-sum contribution in high-income years for immediate tax relief while distributing grants over subsequent years. Legacy planning: the client will includes a charitable legacy of 10% of her residual estate, qualifying the estate for the reduced inheritance tax rate of 36%. The strategy is reviewed annually alongside the broader financial plan.",
            ),
        ),
    )

    val rajPatel = ClientSeed(
        client = CreateClientRequest(
            firstName = "Raj",
            lastName = "Patel",
            email = "raj.patel@neviswealth.com",
            description = "Corporate treasurer exploring ESG-linked financing and green bond issuance.",
        ),
        documents = listOf(
            CreateDocumentRequest(
                title = "Corporate Treasury Policy",
                content = "Treasury management policy for Patel Industries Ltd, effective 1 January 2026. The policy governs the management of the company's cash reserves, short-term investments, and foreign exchange exposures. Cash reserves target: minimum £5,000,000 maintained across three UK clearing banks (Barclays, HSBC, and NatWest) to meet operational requirements. Short-term investment guidelines: surplus cash to be placed in money market funds rated AAA by at least two major rating agencies, with a maximum maturity of 90 days. Foreign exchange policy: all material USD and EUR exposures to be hedged using forward contracts for a rolling 12-month period. Counterparty limits: maximum exposure of £10,000,000 to any single financial institution. The policy is reviewed annually by the board of directors and any amendments require approval from the audit committee.",
            ),
        ),
    )

    val all: List<ClientSeed> = listOf(
        johnDoe, aliceSmith, bobWilliams, mariaGarcia,
        jamesChen, alexandraMorgan, alexThompson, siobhanOConnor, rajPatel,
    )
}
